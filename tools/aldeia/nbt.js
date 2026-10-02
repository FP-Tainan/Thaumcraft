// Leitor e escritor de NBT, o bastante para moldes de estrutura do Minecraft.
// Tudo big-endian, como manda o formato.
const zlib = require('zlib');

const FIM = 0, BYTE = 1, CURTO = 2, INT = 3, LONGO = 4, FLUTUA = 5, DUPLO = 6,
      BYTES = 7, TEXTO = 8, LISTA = 9, COMPOSTO = 10, INTS = 11, LONGOS = 12;

// ------------------------------------------------------------------ ler

class Leitor {
  constructor(buf) { this.b = buf; this.i = 0; }
  byte() { return this.b.readInt8(this.i++); }
  ubyte() { return this.b.readUInt8(this.i++); }
  curto() { const v = this.b.readInt16BE(this.i); this.i += 2; return v; }
  ucurto() { const v = this.b.readUInt16BE(this.i); this.i += 2; return v; }
  int() { const v = this.b.readInt32BE(this.i); this.i += 4; return v; }
  longo() { const v = this.b.readBigInt64BE(this.i); this.i += 8; return v; }
  flutua() { const v = this.b.readFloatBE(this.i); this.i += 4; return v; }
  duplo() { const v = this.b.readDoubleBE(this.i); this.i += 8; return v; }
  texto() { const n = this.ucurto(); const s = this.b.toString('utf8', this.i, this.i + n); this.i += n; return s; }

  valor(tipo) {
    switch (tipo) {
      case BYTE: return this.byte();
      case CURTO: return this.curto();
      case INT: return this.int();
      case LONGO: return this.longo();
      case FLUTUA: return this.flutua();
      case DUPLO: return this.duplo();
      case BYTES: { const n = this.int(); const v = this.b.subarray(this.i, this.i + n); this.i += n; return v; }
      case TEXTO: return this.texto();
      case LISTA: {
        const t = this.ubyte(), n = this.int(), fora = [];
        for (let k = 0; k < n; k++) fora.push(this.valor(t));
        fora.__tipo = t;
        return fora;
      }
      case COMPOSTO: {
        const fora = {};
        for (;;) {
          const t = this.ubyte();
          if (t === FIM) break;
          const nome = this.texto();
          fora[nome] = this.valor(t);
          (fora.__tipos || (fora.__tipos = {}))[nome] = t;
        }
        return fora;
      }
      case INTS: { const n = this.int(), v = []; for (let k = 0; k < n; k++) v.push(this.int()); return v; }
      case LONGOS: { const n = this.int(), v = []; for (let k = 0; k < n; k++) v.push(this.longo()); return v; }
      default: throw new Error('tipo de NBT desconhecido: ' + tipo);
    }
  }
}

function leArquivo(caminho) {
  const fs = require('fs');
  let b = fs.readFileSync(caminho);
  if (b[0] === 0x1f && b[1] === 0x8b) b = zlib.gunzipSync(b);
  const l = new Leitor(b);
  const tipo = l.ubyte();
  if (tipo !== COMPOSTO) throw new Error('raiz não é composto');
  l.texto();
  return l.valor(COMPOSTO);
}

// ------------------------------------------------------------------ escrever

class Escritor {
  constructor() { this.p = []; }
  junta() { return Buffer.concat(this.p); }
  byte(v) { const b = Buffer.alloc(1); b.writeInt8(v); this.p.push(b); }
  ubyte(v) { const b = Buffer.alloc(1); b.writeUInt8(v); this.p.push(b); }
  curto(v) { const b = Buffer.alloc(2); b.writeInt16BE(v); this.p.push(b); }
  ucurto(v) { const b = Buffer.alloc(2); b.writeUInt16BE(v); this.p.push(b); }
  int(v) { const b = Buffer.alloc(4); b.writeInt32BE(v); this.p.push(b); }
  texto(s) { const b = Buffer.from(s, 'utf8'); this.ucurto(b.length); this.p.push(b); }

  // O tipo vem da forma do valor; onde isso é ambíguo usa-se um embrulho.
  tipoDe(v) {
    if (v instanceof Marca) return v.tipo;
    if (typeof v === 'string') return TEXTO;
    if (typeof v === 'number') return Number.isInteger(v) ? INT : DUPLO;
    if (Array.isArray(v)) return LISTA;
    if (v && typeof v === 'object') return COMPOSTO;
    throw new Error('não sei o tipo de ' + v);
  }

  valor(tipo, v) {
    if (v instanceof Marca) v = v.valor;
    switch (tipo) {
      case BYTE: return this.byte(v);
      case CURTO: return this.curto(v);
      case INT: return this.int(v);
      case FLUTUA: { const b = Buffer.alloc(4); b.writeFloatBE(v); this.p.push(b); return; }
      case DUPLO: { const b = Buffer.alloc(8); b.writeDoubleBE(v); this.p.push(b); return; }
      case TEXTO: return this.texto(v);
      case LISTA: {
        const t = v.__tipo !== undefined ? v.__tipo : (v.length ? this.tipoDe(v[0]) : FIM);
        this.ubyte(t); this.int(v.length);
        for (const x of v) this.valor(t, x);
        return;
      }
      case INTS: {
        this.int(v.length);
        for (const x of v) this.int(x);
        return;
      }
      case COMPOSTO: {
        for (const [nome, x] of Object.entries(v)) {
          if (nome.startsWith('__')) continue;
          const t = this.tipoDe(x);
          this.ubyte(t); this.texto(nome); this.valor(t, x);
        }
        this.ubyte(FIM);
        return;
      }
      default: throw new Error('não sei escrever o tipo ' + tipo);
    }
  }
}

/** Embrulho para dizer o tipo quando a forma do valor não basta. */
class Marca {
  constructor(tipo, valor) { this.tipo = tipo; this.valor = valor; }
}

const marca = {
  byte: v => new Marca(BYTE, v),
  int: v => new Marca(INT, v),
  lista: (tipo, itens) => { const a = itens.slice(); a.__tipo = tipo; return a; },
  /** Vetor de inteiros, que é como o BlockPos.CODEC escreve uma posição. */
  ints: v => new Marca(INTS, v),
};

function escreveArquivo(caminho, raiz, comprime = true) {
  const fs = require('fs');
  const e = new Escritor();
  e.ubyte(COMPOSTO); e.texto('');
  e.valor(COMPOSTO, raiz);
  let b = e.junta();
  if (comprime) b = zlib.gzipSync(b);
  fs.writeFileSync(caminho, b);
  return b.length;
}

module.exports = { leArquivo, escreveArquivo, marca, tipos: { BYTE, CURTO, INT, TEXTO, LISTA, COMPOSTO, INTS } };
