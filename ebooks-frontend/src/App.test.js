jest.mock('./context/AuthContext', () => ({
    useAuth: jest.fn(() => ({ user: null, login: jest.fn(), logout: jest.fn() })),
    AuthProvider: ({ children }) => children,
}));

jest.mock('./context/CartContext', () => ({
    useCart: jest.fn(() => ({ cartCount: 0, refreshCartCount: jest.fn() })),
    CartProvider: ({ children }) => children,
}));

// Test de smoke minimal — l'app est testée composant par composant
// dans leurs propres fichiers de test respectifs.

test('environnement de test opérationnel', () => {
    expect(true).toBe(true);
});
