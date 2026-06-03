function uploadForm() {
    return {
        tracks: [],
        audioName: '',
        coverPreview: null,
        submitting: false,

        addTrack() {
            this.tracks.push({ startTime: '', artist: '', title: '' });
        },

        removeTrack(idx) {
            this.tracks.splice(idx, 1);
        },

        previewCover(event) {
            const file = event.target.files[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = e => { this.coverPreview = e.target.result; };
            reader.readAsDataURL(file);
        },

        // === Новый метод: парсинг .cue файла ===
        parseCueFile(event) {
            const file = event.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = (e) => {
                const content = e.target.result;
                this.tracks = this.parseCueContent(content);
            };
            reader.readAsText(file);
        },

        parseCueContent(cueText) {
            const tracks = [];
            const lines = cueText.split('\n');
            let currentTrack = null;
            let globalPerformer = '';

            for (let line of lines) {
                line = line.trim();
                if (!line) continue;

                if (line.startsWith('PERFORMER')) {
                    globalPerformer = line.replace(/^PERFORMER\s+"?|"?\s*$/g, '').trim();
                }

                if (line.startsWith('TRACK')) {
                    if (currentTrack) tracks.push(currentTrack);
                    currentTrack = {
                        startTime: '',
                        artist: globalPerformer,
                        title: ''
                    };
                }

                if (line.startsWith('TITLE') && currentTrack) {
                    currentTrack.title = line.replace(/^TITLE\s+"?|"?\s*$/g, '').trim();
                }

                if (line.startsWith('PERFORMER') && currentTrack) {
                    currentTrack.artist = line.replace(/^PERFORMER\s+"?|"?\s*$/g, '').trim();
                }

                if (line.startsWith('INDEX 01') && currentTrack) {
                    const timeMatch = line.match(/(\d+:\d+:\d+)/);
                    if (timeMatch) {
                        currentTrack.startTime = timeMatch[1].slice(0, 5); // HH:MM
                    }
                }
            }

            if (currentTrack) tracks.push(currentTrack);

            // Если ничего не распарсилось — возвращаем пустой массив
            return tracks.length > 0 ? tracks : [];
        }
    }
}