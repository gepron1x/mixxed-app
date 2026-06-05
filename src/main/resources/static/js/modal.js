const Toast = (() => {
    let container = null;

    function getContainer() {
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function show(message, type = 'error', duration = 4000) {
        const c = getContainer();

        const el = document.createElement('div');
        el.className = `toast toast-${type}`;

        const icon = { error: '✕', success: '✓', warning: '⚠' }[type] ?? '•';

        el.innerHTML = `
            <span class="toast-icon">${icon}</span>
            <span class="toast-msg">${message}</span>
            <button class="toast-close" aria-label="Закрыть">×</button>
        `;

        el.querySelector('.toast-close').addEventListener('click', () => dismiss(el));
        c.appendChild(el);

        // animate in
        requestAnimationFrame(() => requestAnimationFrame(() => el.classList.add('toast-visible')));

        if (duration > 0) {
            setTimeout(() => dismiss(el), duration);
        }

        return el;
    }

    function dismiss(el) {
        if (!el || !el.parentNode) return;
        el.classList.add('toast-hiding');
        el.addEventListener('transitionend', () => el.remove(), { once: true });
    }

    return { show };
})();

const Confirm = (() => {
    function show(message, confirmLabel = 'Удалить') {
        return new Promise(resolve => {
            const backdrop = document.createElement('div');
            backdrop.className = 'modal-backdrop';
            backdrop.setAttribute('role', 'dialog');
            backdrop.setAttribute('aria-modal', 'true');

            backdrop.innerHTML = `
                <div class="modal-dialog">
                    <p class="modal-message">${message}</p>
                    <div class="modal-actions">
                        <button class="btn-secondary modal-btn-cancel">Отмена</button>
                        <button class="btn-danger modal-btn-confirm">${confirmLabel}</button>
                    </div>
                </div>
            `;

            document.body.appendChild(backdrop);
            document.body.style.overflow = 'hidden';

            requestAnimationFrame(() => requestAnimationFrame(() => backdrop.classList.add('modal-visible')));

            setTimeout(() => backdrop.querySelector('.modal-btn-confirm')?.focus(), 80);

            function close(result) {
                backdrop.classList.remove('modal-visible');
                document.body.style.overflow = '';
                backdrop.addEventListener('transitionend', () => backdrop.remove(), { once: true });
                resolve(result);
            }

            backdrop.querySelector('.modal-btn-cancel').addEventListener('click', () => close(false));
            backdrop.querySelector('.modal-btn-confirm').addEventListener('click', () => close(true));

            // close on backdrop click
            backdrop.addEventListener('click', e => { if (e.target === backdrop) close(false); });

            // close on Escape
            function onKey(e) {
                if (e.key === 'Escape') { document.removeEventListener('keydown', onKey); close(false); }
            }
            document.addEventListener('keydown', onKey);
        });
    }

    return { show };
})();


window.showToast   = (msg, type = 'error', duration = 4000) => Toast.show(msg, type, duration);
window.showConfirm = (msg, label = 'Удалить') => Confirm.show(msg, label);
