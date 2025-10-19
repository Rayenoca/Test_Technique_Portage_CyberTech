/**
 * URL Shortener Application
 * JavaScript pour la gestion des interactions utilisateur
 */

class UrlShortener {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.setupFormValidation();
    }

    bindEvents() {
        // Bouton raccourcir
        const shortenBtn = document.getElementById('shortenBtn');
        if (shortenBtn) {
            shortenBtn.addEventListener('click', () => this.shortenUrl());
        }

        // Bouton récupérer
        const expandBtn = document.getElementById('expandBtn');
        if (expandBtn) {
            expandBtn.addEventListener('click', () => this.expandUrl());
        }

        // Entrée clavier pour les champs
        const originalUrlInput = document.getElementById('originalUrl');
        const shortCodeInput = document.getElementById('shortCode');

        if (originalUrlInput) {
            originalUrlInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.shortenUrl();
                }
            });
        }

        if (shortCodeInput) {
            shortCodeInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.expandUrl();
                }
            });
        }
    }

    setupFormValidation() {
        // Validation en temps réel
        const inputs = document.querySelectorAll('input[type="text"]');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                this.clearResults(input.closest('.section'));
            });
        });
    }

    async shortenUrl() {
        const input = document.getElementById('originalUrl');
        const button = document.getElementById('shortenBtn');
        const section = input.closest('.section');
        
        if (!this.validateInput(input, 'Veuillez saisir une URL valide.')) {
            return;
        }

        this.setLoading(button, true);
        this.clearResults(section);

        try {
            const response = await fetch('/api/shorten', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    originalUrl: input.value.trim() 
                })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Erreur lors du raccourcissement.');
            }

            const data = await response.json();
            const shortUrl = data.shortUrl || data.short || data.url;
            
            if (shortUrl) {
                this.showResult(section, 'success', 
                    `URL raccourcie générée avec succès !`, 
                    shortUrl
                );
                this.copyToClipboard(shortUrl);
            } else {
                throw new Error('Réponse inattendue du serveur.');
            }

        } catch (error) {
            this.showResult(section, 'error', error.message || 'Erreur inattendue.');
        } finally {
            this.setLoading(button, false);
        }
    }

    async expandUrl() {
        const input = document.getElementById('shortCode');
        const button = document.getElementById('expandBtn');
        const section = input.closest('.section');
        
        if (!this.validateInput(input, 'Veuillez saisir un code court ou une URL courte.')) {
            return;
        }

        this.setLoading(button, true);
        this.clearResults(section);

        try {
            const rawValue = input.value.trim();
            const shortCode = this.extractCode(rawValue);
            
            if (!shortCode) {
                throw new Error('Impossible d\'extraire le code court.');
            }

            const response = await fetch(`/api/expand/${encodeURIComponent(shortCode)}`, {
                method: 'GET',
                redirect: 'manual'
            });

            // Gestion des redirections (fallback)
            if (response.status >= 300 && response.status < 400) {
                const location = response.headers.get('Location');
                if (location) {
                    this.showResult(section, 'success', 
                        `URL originale trouvée !`, 
                        location
                    );
                    return;
                }
            }

            if (response.ok) {
                const data = await response.json().catch(() => null);
                const originalUrl = data && (data.originalUrl || data.url || data.full);
                
                if (originalUrl) {
                    this.showResult(section, 'success', 
                        `URL originale récupérée !`, 
                        originalUrl
                    );
                } else {
                    throw new Error('Aucune URL trouvée pour ce code.');
                }
            } else if (response.status === 404) {
                throw new Error('Code inconnu - cette URL courte n\'existe pas.');
            } else {
                throw new Error(`Erreur du serveur (${response.status})`);
            }

        } catch (error) {
            this.showResult(section, 'error', error.message || 'Erreur inattendue.');
        } finally {
            this.setLoading(button, false);
        }
    }

    extractCode(input) {
        const value = (input || '').trim();
        if (!value) return '';

        try {
            const url = new URL(value);
            const path = url.pathname.replace(/\/+$/, ''); // Supprimer les / en fin
            const parts = path.split('/').filter(Boolean);
            return parts.length ? parts[parts.length - 1] : '';
        } catch (e) {
            // Pas une URL complète, considérer comme code direct
            return value;
        }
    }

    validateInput(input, message) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, message);
            return false;
        }
        this.clearError(input);
        return true;
    }

    showError(input, message) {
        this.clearError(input);
        const errorDiv = document.createElement('div');
        errorDiv.className = 'result error show';
        errorDiv.textContent = message;
        input.parentNode.appendChild(errorDiv);
        input.style.borderColor = 'var(--error-color)';
    }

    clearError(input) {
        const existingError = input.parentNode.querySelector('.result.error');
        if (existingError) {
            existingError.remove();
        }
        input.style.borderColor = '';
    }

    showResult(section, type, message, url = null) {
        const resultDiv = section.querySelector(`.result.${type}`);
        if (resultDiv) {
            resultDiv.innerHTML = url ? 
                `${message}<br><a href="${url}" target="_blank" rel="noopener">${url}</a>` : 
                message;
            resultDiv.classList.add('show');
        }
    }

    clearResults(section) {
        const results = section.querySelectorAll('.result');
        results.forEach(result => {
            result.classList.remove('show');
            result.innerHTML = '';
        });
    }

    setLoading(button, isLoading) {
        if (isLoading) {
            button.disabled = true;
            button.innerHTML = '<span class="loading"></span> Traitement...';
        } else {
            button.disabled = false;
            button.innerHTML = button.dataset.originalText || 'Traiter';
        }
    }

    async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            this.showToast('URL copiée dans le presse-papiers !', 'success');
        } catch (err) {
            // Fallback pour les navigateurs plus anciens
            const textArea = document.createElement('textarea');
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
            this.showToast('URL copiée dans le presse-papiers !', 'success');
        }
    }

    showToast(message, type = 'info') {
        // Créer un toast temporaire
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: var(--${type === 'success' ? 'success' : 'primary'}-color);
            color: white;
            padding: 1rem 1.5rem;
            border-radius: var(--radius-sm);
            box-shadow: var(--shadow-lg);
            z-index: 1000;
            animation: slideIn 0.3s ease-out;
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
}

// Initialisation de l'application
document.addEventListener('DOMContentLoaded', () => {
    new UrlShortener();
    
    // Stocker le texte original des boutons
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(btn => {
        btn.dataset.originalText = btn.textContent;
    });
});

// Animation CSS pour les toasts
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        from {
            opacity: 1;
            transform: translateX(0);
        }
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
`;
document.head.appendChild(style);
