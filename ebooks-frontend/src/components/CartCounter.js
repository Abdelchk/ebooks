import React from 'react';
import { Badge } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const CartCounter = () => {
  const { user } = useAuth();
  const { cartCount } = useCart();

  if (!user || cartCount === 0) return null;

  return (
    <Badge bg="danger" pill className="ms-1 cart-counter-badge">
      {cartCount}
    </Badge>
  );
};

export default CartCounter;
