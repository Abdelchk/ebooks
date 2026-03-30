import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { bookService } from '../services/bookService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import './Accueil.css';

const Accueil = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadBooks();
  }, []);

  const loadBooks = async () => {
    try {
      const data = await bookService.getAllBooks();
      setBooks(data);
    } catch (err) {
      setError('Erreur lors du chargement des livres.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleBookClick = (bookId) => {
    navigate(`/book/${bookId}`);
  };

  return (
    <>
      <Navigation />
      <Container className="mt-4">
        <h2>Liste des livres</h2>

        {error && <Alert variant="danger">{error}</Alert>}

        {loading ? (
          <Loader message="Chargement des livres..." />
        ) : (
          <Row>
            {books.map((book) => (
              <Col md={4} key={book.id} className="d-flex mb-3">
                <Card 
                  className="w-100 h-100 d-flex flex-column book-card" 
                  onClick={() => handleBookClick(book.id)}
                  style={{ cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s' }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.12)';
                  }}
                >
                  <Card.Img
                    variant="top"
                    src={book.coverImageUrl}
                    alt="Couverture du livre"
                    style={{ height: '200px', objectFit: 'cover' }}
                  />
                  <Card.Body className="d-flex flex-column">
                    <Card.Title>{book.title}</Card.Title>
                    <Card.Text className="flex-grow-1">{book.description}</Card.Text>
                    <Card.Text>
                      <small className="text-muted">Auteur : {book.author}</small>
                    </Card.Text>
                    <Card.Text>
                      <small className="text-muted">Stock : {book.quantity}</small>
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Container>
    </>
  );
};

export default Accueil;


