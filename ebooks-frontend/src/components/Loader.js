import React from 'react';

const Loader = ({ message = 'Chargement...' }) => {
  return (
    <div className="text-center mt-5">
      <div className="loader"></div>
      <p className="mt-3">{message}</p>
    </div>
  );
};

export default Loader;

