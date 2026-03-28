import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Alert } from 'react-bootstrap';
import { bookService } from '../services/bookService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';

const Accueil = () => {
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
                <Card className="w-100 h-100 d-flex flex-column">
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


