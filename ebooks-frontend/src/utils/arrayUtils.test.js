import { toArray } from './arrayUtils';

describe('toArray', () => {

  // ── Cas : tableau simple ────────────────────────────────────────────────────
  it('retourne le tableau tel quel si data est déjà un tableau', () => {
    const input = [1, 2, 3];
    expect(toArray(input)).toBe(input);
  });

  it('retourne un tableau vide si data est un tableau vide', () => {
    expect(toArray([])).toEqual([]);
  });

  it('retourne un tableau d\'objets tel quel', () => {
    const books = [{ id: 1, title: 'Livre A' }, { id: 2, title: 'Livre B' }];
    expect(toArray(books)).toBe(books);
  });

  // ── Cas : Spring Page object ────────────────────────────────────────────────
  it('extrait content si data est un objet Spring Page', () => {
    const page = {
      content: [{ id: 1 }, { id: 2 }],
      totalElements: 2,
      totalPages: 1,
      number: 0,
    };
    expect(toArray(page)).toEqual([{ id: 1 }, { id: 2 }]);
  });

  it('retourne un tableau vide si content est un tableau vide dans Spring Page', () => {
    const page = { content: [], totalElements: 0, totalPages: 0 };
    expect(toArray(page)).toEqual([]);
  });

  it('ignore content si ce n\'est pas un tableau', () => {
    const data = { content: 'pas un tableau' };
    expect(toArray(data)).toEqual([]);
  });

  // ── Cas : valeurs nulles / undefined / invalides ────────────────────────────
  it('retourne [] si data est null', () => {
    expect(toArray(null)).toEqual([]);
  });

  it('retourne [] si data est undefined', () => {
    expect(toArray(undefined)).toEqual([]);
  });

  it('retourne [] si data est un nombre', () => {
    expect(toArray(42)).toEqual([]);
  });

  it('retourne [] si data est une chaîne', () => {
    expect(toArray('texte')).toEqual([]);
  });

  it('retourne [] si data est un objet sans propriété content', () => {
    expect(toArray({ message: 'error', status: 500 })).toEqual([]);
  });

  it('retourne [] si data est false', () => {
    expect(toArray(false)).toEqual([]);
  });
});

