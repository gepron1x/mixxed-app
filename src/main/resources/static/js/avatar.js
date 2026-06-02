function avatarPreview() {
        return {
            previewUrl: null,

            previewFile(event) {
                const file = event.target.files[0];
                if (!file) return;

                if (!file.type.startsWith('image/')) {
                    alert('Пожалуйста, выберите изображение');
                    event.target.value = '';
                    return;
                }

                this.previewUrl = URL.createObjectURL(file);
            },

            cancelPreview() {
                this.previewUrl = null;
                const input = document.querySelector('input[name="avatarFile"]');
                if (input) input.value = '';
            }
        }
    }