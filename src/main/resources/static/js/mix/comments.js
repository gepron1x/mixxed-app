function commentsManager(comments) {
    return {
        comments: comments,   // CommentDTO[]
        newComment: '',

        formatDate(dateStr) {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return date.toLocaleDateString('ru-RU', {
                day: '2-digit', month: '2-digit', year: 'numeric',
                hour: '2-digit', minute: '2-digit'
            });
        },

        async addComment(event) {
            const form = event.target;
            const formData = new FormData(form);

            try {
                const response = await fetch(form.action, { method: 'POST', body: formData });

                if (response.ok) {
                    const newCommentData = await response.json();
                    this.comments.unshift(newCommentData);
                    this.newComment = '';
                } else {
                    showToast('Ошибка при добавлении комментария', 'error');
                }
            } catch (e) {
                console.error(e);
                showToast('Не удалось отправить комментарий', 'error');
            }
        },

        async deleteComment(commentId) {
            const confirmed = await showConfirm('Удалить комментарий?', 'Удалить');
            if (!confirmed) return;

            try {
                const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

                const response = await fetch(`/comment/${commentId}`, {
                    method: 'DELETE',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });

                if (response.ok) {
                    this.comments = this.comments.filter(c => c.id !== commentId);
                    showToast('Комментарий удалён', 'success');
                } else if (response.status === 403) {
                    showToast('У вас нет прав для удаления этого комментария', 'warning');
                } else {
                    showToast('Ошибка при удалении', 'error');
                }
            } catch (e) {
                console.error(e);
                showToast('Не удалось удалить комментарий', 'error');
            }
        }
    }
}
