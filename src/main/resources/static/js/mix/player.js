function audioPlayer(initData) {
    return {
        playing: false,
        currentTime: 0,
        duration: 0,
        progress: 0,
        volume: 0.8,
        activeTrack: null,
        liked: initData.liked || false,
        likeCount: initData.likeCount || 0,
        slug: initData.slug || '',

        getAudio() {
            return this.$refs.audio;
        },

        togglePlay() {
            const audio = this.getAudio();
            if (!audio.src) {
                audio.src = '/api/mixes/' + this.slug + '/stream';
                fetch('/api/mixes/' + this.slug + '/play', { method: 'POST' }).catch(() => {});
            }
            if (this.playing) {
                audio.pause();
                this.playing = false;
            } else {
                audio.play().then(() => { this.playing = true; }).catch(() => {});
            }
        },

        onTimeUpdate() {
            const audio = this.getAudio();
            this.currentTime = audio.currentTime;
            this.progress = this.duration > 0 ? (audio.currentTime / this.duration) * 100 : 0;
        },

        onLoaded() {
            this.duration = this.getAudio().duration || 0;
            this.getAudio().volume = this.volume;
        },

        seek(event) {
            const bar = event.currentTarget;
            const rect = bar.getBoundingClientRect();
            const pct = (event.clientX - rect.left) / rect.width;
            const audio = this.getAudio();
            if (audio.src && this.duration > 0) {
                audio.currentTime = pct * this.duration;
            }
        },

        seekToTime(seconds, trackNum) {
            const audio = this.getAudio();
            if (!audio.src) {
                audio.src = '/api/mixes/' + this.slug + '/stream';
                fetch('/api/mixes/' + this.slug + '/play', { method: 'POST' }).catch(() => {});
            }
            this.activeTrack = trackNum;
            audio.currentTime = seconds;
            audio.play().then(() => { this.playing = true; }).catch(() => {});
        },

        setVolume() {
            this.getAudio().volume = this.volume;
        },

        formatTime(secs) {
            if (!secs || isNaN(secs)) return '0:00';
            const m = Math.floor(secs / 60);
            const s = Math.floor(secs % 60);
            return m + ':' + (s < 10 ? '0' : '') + s;
        },

        toggleLike() {
            fetch('/api/mixes/' + this.slug + '/like', { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    this.liked = data.liked;
                    this.likeCount = data.count;
                }).catch(() => {});
        }
    }
}