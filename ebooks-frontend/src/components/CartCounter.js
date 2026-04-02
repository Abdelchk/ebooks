import React, { useState, useEffect } from 'react';
import { Badge } from 'react-bootstrap';
import { cartService } from '../services/cartService';
import { useAuth } from '../context/AuthContext';

const CartCounter = () => {
  const [count, setCount] = useState(0);
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      loadCartCount();
    }
  }, [user]);

  const loadCartCount = async () => {
    try {
      const count = await cartService.getCartCount();
      setCount(count);
    } catch (error) {
      console.error('Erreur chargement compteur panier:', error);
      setCount(0);
    }
  };

  if (!user || count === 0) {
    return null;
  }

  return (
    <Badge bg="danger" pill className="ms-1 cart-counter-badge">
      {count}
    </Badge>
  );
};

export default CartCounter;

