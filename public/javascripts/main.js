// Minimal JS for current views
document.addEventListener('DOMContentLoaded', () => {
    initNotifications();
    initDeleteConfirmations();
});

function initNotifications() {
    document.querySelectorAll('[data-notification]').forEach(n => {
        const close = n.querySelector('[data-notification-close]');
        if (close) close.addEventListener('click', () => dismiss(n));
        setTimeout(() => dismiss(n), 3000);
    });
    function dismiss(n) {
        if (!n || !n.parentNode) return;
        n.classList.add('is-dismissing');
        setTimeout(() => { if (n.parentNode) n.remove(); }, 200);
    }
}

function initDeleteConfirmations() {
    document.querySelectorAll('.delete-form').forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm('Are you sure you want to delete this?')) e.preventDefault();
        });
    });
}

