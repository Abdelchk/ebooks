import React from 'react';
import { Button, ButtonGroup } from 'react-bootstrap';
import './LoanDurationSelector.css';

const LoanDurationSelector = ({ value, onChange }) => {
  const durations = [7, 14, 21, 30];

  return (
    <div className="loan-duration-selector">
      <ButtonGroup>
        {durations.map((duration) => (
          <Button
            key={duration}
            variant={value === duration ? 'primary' : 'outline-primary'}
            onClick={() => onChange(duration)}
            className="duration-btn"
          >
            <i className="bi bi-calendar-event me-1"></i>
            {duration} jours
          </Button>
        ))}
      </ButtonGroup>
      <small className="text-muted d-block mt-2">
        <i className="bi bi-info-circle me-1"></i>
        Vous pouvez prolonger jusqu'à 2 fois (+7 jours à chaque fois)
      </small>
    </div>
  );
};

export default LoanDurationSelector;

