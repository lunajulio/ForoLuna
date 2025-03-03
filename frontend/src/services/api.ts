import axios from 'axios';

const API_URL = 'http://localhost:8080'; // ajusta al puerto de tu backend

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});