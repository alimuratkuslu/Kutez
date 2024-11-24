import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ProductCard from './ProductCard';
import '../styles/ProductList.css';

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [popularityRange, setPopularityRange] = useState({ min: '', max: '' });
  const [showFilters, setShowFilters] = useState(false);

  useEffect(() => {
    fetchProducts();
  }, [priceRange, popularityRange]);

  const fetchProducts = async () => {
    try {
      let url = 'https://akuslu-kutez-763bd8b2f460.herokuapp.com/api/product';
      const params = new URLSearchParams();
      
      if (priceRange.min) params.append('minPrice', priceRange.min);
      if (priceRange.max) params.append('maxPrice', priceRange.max);
      if (popularityRange.min) params.append('minPopularity', popularityRange.min);
      if (popularityRange.max) params.append('maxPopularity', popularityRange.max);
      
      if (params.toString()) {
        url += '?' + params.toString();
      }

      const response = await axios.get(url);
      setProducts(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch products: ' + err.message);
      setLoading(false);
    }
  };

  const handlePriceChange = (type, value) => {
    setPriceRange(prev => ({
      ...prev,
      [type]: value
    }));
  };

  const handlePopularityChange = (type, value) => {
    setPopularityRange(prev => ({
      ...prev,
      [type]: value
    }));
  };

  const clearFilters = () => {
    setPriceRange({ min: '', max: '' });
    setPopularityRange({ min: '', max: '' });
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="product-list-container">
      <div className="product-list-header">
        <h2>Product List</h2>
        <button 
          className="filter-toggle"
          onClick={() => setShowFilters(!showFilters)}
        >
          {showFilters ? 'Hide Filters' : 'Show Filters'}
        </button>
      </div>
      
      {showFilters && (
        <div className="filters-container">
          <div className="filter-group">
            <h3>Price Range</h3>
            <div className="filter-inputs">
              <input
                type="number"
                placeholder="Min Price"
                value={priceRange.min}
                onChange={(e) => handlePriceChange('min', e.target.value)}
              />
              <span>-</span>
              <input
                type="number"
                placeholder="Max Price"
                value={priceRange.max}
                onChange={(e) => handlePriceChange('max', e.target.value)}
              />
            </div>
          </div>
          
          <div className="filter-group">
            <h3>Rating (0-5)</h3>
            <div className="filter-inputs">
              <input
                type="number"
                placeholder="Min Rating"
                min="0"
                max="5"
                step="0.1"
                value={popularityRange.min}
                onChange={(e) => {
                    const value = Math.min(Math.max(0, parseFloat(e.target.value) || 0), 5);
                    handlePopularityChange('min', value);
                }}
              />
              <span>-</span>
              <input
                type="number"
                placeholder="Max Rating"
                min="0"
                max="5"
                step="0.1"
                value={popularityRange.max}
                onChange={(e) => {
                    const value = Math.min(Math.max(0, parseFloat(e.target.value) || 0), 5);
                    handlePopularityChange('max', value);
                }}
              />
            </div>
          </div>
          
          <button className="clear-filters" onClick={clearFilters}>
            Clear Filters
          </button>
        </div>
      )}

      <div className="product-list-scroll">
        {products.map((product, index) => (
          <ProductCard key={index} product={product} />
        ))}
      </div>
    </div>
  );
};

export default ProductList;