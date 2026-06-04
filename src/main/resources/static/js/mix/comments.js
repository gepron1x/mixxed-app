function commentsManager(comments) {
        return {
            comments: comments,   // CommentDTO[]
            newComment: '',

            formatDate(dateStr) {
                if (!dateStr) return '';
                const date = new Date(dateStr);
                return date.toLocaleDateString('ru-RU', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            },

            async addComment(event) {
                const form = event.target;
                const formData = new FormData(form);

                try {
                    const response = await fetch(form.action, {
                        method: 'POST',
                        body: formData
                    });

                    if (response.ok) {
                        const newCommentData = await response.json(); // предполагаем, что бэкенд возвращает созданный CommentDTO
                        this.comments.unshift(newCommentData);
                        this.newComment = '';
                    } else {
                        alert('Ошибка при добавлении комментария');
                    }
                } catch (e) {
                    console.error(e);
                    alert('Не удалось отправить комментарий');
                }
            },

            async deleteComment(commentId) {
                if (!confirm('Удалить комментарий?')) return;

                try {
                    const csrfToken = document.querySelector('input[name="_csrf"]').value;

                    const response = await fetch(`/comment/${commentId}`, {
                        method: 'DELETE',
                        headers: {
                            'X-CSRF-TOKEN': csrfToken
                        }
                    });

                    if (response.ok) {
                        this.comments = this.comments.filter(c => c.id !== commentId);
                    } else if (response.status === 403) {
                        alert('У вас нет прав для удаления этого комментария');
                    } else {
                        alert('Ошибка при удалении');
                    }
                } catch (e) {
                    console.error(e);
                    alert('Не удалось удалить комментарий');
                }
            }
        }
    }