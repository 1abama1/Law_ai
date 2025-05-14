import React, { useState } from 'react';
import api from '../api.js';

const Loan = ({ userId }) => {
    const [amount, setAmount] = useState('');
    const [interestRate, setInterestRate] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [loanUpdated, setLoanUpdated] = useState(false);

    const handleLoanRequest = async (e) => {
        e.preventDefault();

        try {
            const loanData = {
                amount: parseFloat(amount),
                interestRate: parseFloat(interestRate),
                userId: userId,
                termInMonths: 12 // Можно добавить выбор срока кредита
            };

            const response = await api.post('/api/loans', loanData);
            
            if (response.status === 200) {
                setSuccessMessage('Кредит успешно запрошен!');
                setErrorMessage('');
                setAmount('');
                setInterestRate('');
                setLoanUpdated(!loanUpdated);
            }
        } catch (error) {
            const serverError = error.response?.data?.message;
            if (serverError) {
                setErrorMessage(serverError);
            } else if (error.message.includes('Network Error')) {
                setErrorMessage('Проблемы с соединением');
            } else {
                setErrorMessage('Ошибка сервера');
            }
        }
    };

    return (
        <div className="loan-container">
            <h2>Запрос кредита</h2>
            <form onSubmit={handleLoanRequest} className="loan-form">
                <div className="form-group">
                    <label htmlFor="amount">Сумма кредита:</label>
                    <input
                        id="amount"
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="Введите сумму"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value.replace(/[^0-9.]/g, ''))}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="interestRate">Процентная ставка:</label>
                    <input
                        id="interestRate"
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="Введите процентную ставку"
                        value={interestRate}
                        onChange={(e) => setInterestRate(e.target.value.replace(/[^0-9.]/g, ''))}
                        required
                    />
                </div>

                <button type="submit" className="submit-button">Запросить кредит</button>
            </form>

            {successMessage && <p className="success-message">{successMessage}</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
};

export default Loan; 