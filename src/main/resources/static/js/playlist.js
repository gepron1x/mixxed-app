function playlistPlayer(initialMixes = []) {
    return {
        mixes: initialMixes,
        currentIndex: null,
        currentMix: null,
        playing: false,
        currentTime: 0,
        duration: 0,
        progress: 0,
        audioEl: null,

        initAudio() {
            this.audioEl = this.$refs.audio || this.$el.querySelector('audio');
        },

        playAll() {
            if (this.mixes.length > 0) {
                this.playAt(0);
            }
        },

        playAt(index) {
            if (index < 0 || index >= this.mixes.length) return;

            if (this.currentIndex === index) {
                this.togglePlay();
                return;
            }

            if (!this.audioEl) this.initAudio();

            const mix = this.mixes[index];
            this.currentIndex = index;
            this.currentMix = mix;

            this.audioEl.src = '/api/mixes/' + mix.slug + '/stream';

            fetch('/api/mixes/' + mix.slug + '/play', { method: 'POST' }).catch(() => {});

            this.audioEl.play()
                .then(() => { this.playing = true; })
                .catch((err) => { console.error("Playback failed:", err); });
        },

        togglePlay() {
            if (!this.audioEl) this.initAudio();
            if (this.currentIndex === null && this.mixes.length > 0) {
                this.playAt(0);
                return;
            }

            if (this.playing) {
                this.audioEl.pause();
                this.playing = false;
            } else {
                this.audioEl.play()
                    .then(() => { this.playing = true; })
                    .catch(() => {});
            }
        },

        nextTrack() {
            const next = (this.currentIndex ?? -1) + 1;
            if (next < this.mixes.length) {
                this.playAt(next);
            } else {
                this.playing = false;
                this.currentIndex = null;
                this.currentMix = null;
            }
        },

        prevTrack() {
                    const prev = (this.currentIndex ?? -1) - 1;
                    if (prev >= 0) {
                        this.playAt(prev);
                    } else {
                        this.playing = false;
                        this.currentIndex = null;
                        this.currentMix = null;
                    }
                },

        onTimeUpdate() {
            if (!this.audioEl) return;
            this.currentTime = this.audioEl.currentTime;
            this.progress = this.duration > 0 ? (this.audioEl.currentTime / this.duration) * 100 : 0;
        },

        seek(event) {
            if (!this.audioEl || this.duration === 0) return;
            const rect = event.currentTarget.getBoundingClientRect();
            const pct = (event.clientX - rect.left) / rect.width;
            this.audioEl.currentTime = pct * this.duration;
        },

        formatTime(secs) {
            if (!secs || isNaN(secs)) return '0:00';
            const m = Math.floor(secs / 60);
            const s = Math.floor(secs % 60);
            return m + ':' + (s < 10 ? '0' : '') + s;
        }
    }
}