/**
 * Normalise une réponse API en tableau.
 * Gère les cas :
 *  - tableau simple         → retourné tel quel
 *  - Spring Page { content: [...] } → retourne content
 *  - null / undefined / autre objet → retourne []
 */
export const toArray = (data) => {
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.content)) return data.content;
  return [];
};

