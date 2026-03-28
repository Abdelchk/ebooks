import { validateEmail, validatePassword, validatePhoneNumber, validateName, getPasswordStrength } from '../utils/validation';

describe('Validation Utils', () => {
  describe('validateEmail', () => {
    test('should validate correct email', () => {
      expect(validateEmail('test@example.com')).toBe(true);
      expect(validateEmail('user.name@domain.co.uk')).toBe(true);
    });

    test('should reject invalid email', () => {
      expect(validateEmail('invalid-email')).toBe(false);
      expect(validateEmail('@example.com')).toBe(false);
      expect(validateEmail('test@')).toBe(false);
    });
  });

  describe('validatePassword', () => {
    test('should validate strong password', () => {
      expect(validatePassword('MotDePasse123@')).toBe(true);
      expect(validatePassword('Azerty123456#')).toBe(true);
    });

    test('should reject weak password', () => {
      expect(validatePassword('short')).toBe(false);
      expect(validatePassword('nospecialchar123')).toBe(false);
      expect(validatePassword('NoDigits@@@')).toBe(false);
    });
  });

  describe('validatePhoneNumber', () => {
    test('should validate French phone number', () => {
      expect(validatePhoneNumber('0612345678')).toBe(true);
      expect(validatePhoneNumber('+33612345678')).toBe(true);
    });

    test('should reject invalid phone number', () => {
      expect(validatePhoneNumber('123')).toBe(false);
      expect(validatePhoneNumber('abcdefghij')).toBe(false);
    });
  });

  describe('validateName', () => {
    test('should validate correct name', () => {
      expect(validateName('Jean')).toBe(true);
      expect(validateName('Jean-Pierre')).toBe(true);
      expect(validateName('Marie Ève')).toBe(true);
    });

    test('should reject invalid name', () => {
      expect(validateName('J')).toBe(false);
      expect(validateName('Jean123')).toBe(false);
      expect(validateName('')).toBe(false);
    });
  });

  describe('getPasswordStrength', () => {
    test('should return weak for short password', () => {
      const strength = getPasswordStrength('short');
      expect(strength.level).toBe('weak');
    });

    test('should return strong for complex password', () => {
      const strength = getPasswordStrength('VeryStrongPassword123@#$');
      expect(strength.level).toBe('strong');
    });
  });
});

