import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Alert, Form, InputGroup, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { bookService } from '../services/bookService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import './Accueil.css';

const Accueil = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [filteredBooks, setFilteredBooks] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  // Liste des catégories (basée sur la base de données)
  const categories = [
    'Biographie',
    'Education',
    'Essai',
    'Fantasy',
    'Fiction littéraire',
    'Histoire',
    'Jeunesse',
    'Philosophie',
    'Polar',
    'Roman',
    'Romance',
    'Science-Fiction',
    'Thriller'
  ];

  const filterBooks = useCallback(() => {
    let filtered = books;

    // Filtre par catégorie
    if (selectedCategory) {
      filtered = filtered.filter(book => book.category === selectedCategory);
    }

    // Filtre par recherche (titre ou auteur)
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(book =>
        book.title.toLowerCase().includes(query) ||
        book.author.toLowerCase().includes(query)
      );
    }

    setFilteredBooks(filtered);
  }, [books, selectedCategory, searchQuery]);

  useEffect(() => {
    loadBooks();
  }, []);

  useEffect(() => {
    filterBooks();
  }, [filterBooks]);

  const loadBooks = async () => {
    try {
      const data = await bookService.getAllBooks();
      setBooks(data);
      setFilteredBooks(data);
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

  const handleCategoryClick = (category) => {
    setSelectedCategory(category === selectedCategory ? '' : category);
  };

  return (
    <>
      <Navigation />
      <Container fluid className="mt-4">
        <Row>
          {/* Sidebar avec catégories */}
          <Col md={3} lg={2} className="mb-4">
            <h5 className="mb-3">Catégories</h5>
            <ListGroup>
              <ListGroup.Item
                action
                active={selectedCategory === ''}
                onClick={() => setSelectedCategory('')}
                style={{ cursor: 'pointer' }}
              >
                Toutes les catégories
              </ListGroup.Item>
              {categories.map((category, index) => (
                <ListGroup.Item
                  key={index}
                  action
                  active={selectedCategory === category}
                  onClick={() => handleCategoryClick(category)}
                  style={{ cursor: 'pointer' }}
                >
                  {category}
                </ListGroup.Item>
              ))}
            </ListGroup>
          </Col>

          {/* Contenu principal */}
          <Col md={9} lg={10}>
            <h2>Liste des livres</h2>

            {/* Barre de recherche */}
            <Form className="mb-4">
              <InputGroup>
                <Form.Control
                  type="text"
                  placeholder="Rechercher par titre ou auteur..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <InputGroup.Text>
                  <i className="bi bi-search"></i>
                </InputGroup.Text>
              </InputGroup>
            </Form>

            {selectedCategory && (
              <Alert variant="info" dismissible onClose={() => setSelectedCategory('')}>
                Catégorie sélectionnée : <strong>{selectedCategory}</strong>
              </Alert>
            )}

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
              <Loader message="Chargement des livres..." />
            ) : (
              <>
                {filteredBooks.length === 0 ? (
                  <Alert variant="warning">
                    Aucun livre trouvé {searchQuery ? `pour "${searchQuery}"` : ''}
                    {selectedCategory ? ` dans la catégorie "${selectedCategory}"` : ''}.
                  </Alert>
                ) : (
                  <Row>
                    {filteredBooks.map((book) => (
                      <Col md={6} lg={4} xl={3} key={book.id} className="mb-4">
                        <Card
                          className="book-card shadow-sm h-100"
                          onClick={() => handleBookClick(book.id)}
                        >
                          <div className="book-cover-wrapper">
                            <Card.Img
                              variant="top"
                              src={book.coverImageUrl}
                              alt={`Couverture de ${book.title}`}
                              className="book-cover-img"
                            />
                          </div>
                          <Card.Body className="d-flex flex-column">
                            <Card.Title className="book-title mb-2">{book.title}</Card.Title>
                            <Card.Text className="book-description flex-grow-1 text-muted mb-2">
                              {book.description}
                            </Card.Text>
                            <div className="book-meta mt-auto">
                              <div className="d-flex align-items-center mb-1">
                                <i className="bi bi-person-fill me-2 text-primary"></i>
                                <small className="text-dark"><strong>{book.author}</strong></small>
                              </div>
                              <div className="d-flex align-items-center">
                                <i className="bi bi-box-seam me-2 text-success"></i>
                                <small className="text-muted">Stock : {book.quantity}</small>
                              </div>
                            </div>
                          </Card.Body>
                        </Card>
                      </Col>
                    ))}
                  </Row>
                )}
              </>
            )}
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default Accueil;


