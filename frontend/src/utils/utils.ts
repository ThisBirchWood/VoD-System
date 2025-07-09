function formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);

    const padded = (n: number) => n.toString().padStart(2, '0');

    if (h > 0) {
        return `${h}:${padded(m)}:${padded(s)}`;
    } else {
        return `${m}:${padded(s)}`;
    }
}

export {
    formatTime
}