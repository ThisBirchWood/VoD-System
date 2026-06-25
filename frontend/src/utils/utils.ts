function formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    const pad = (n: number) => n.toString().padStart(2, '0');
    if (h > 0) return `${h}:${pad(m)}:${pad(s)}`;
    return `${m}:${pad(s)}`;
}

function stringToDate(dateString: string): Date {
    const date = new Date(dateString.substring(0, 23));
    if (isNaN(date.getTime())) throw new Error("Invalid date string");
    return date;
}

function dateToTimeAgo(date: Date): string {
    const secondsAgo = Math.floor((Date.now() - date.getTime()) / 1000);
    const units: [number, string][] = [
        [31536000, 'year'],
        [2592000, 'month'],
        [86400, 'day'],
        [3600, 'hour'],
        [60, 'minute'],
    ];
    for (const [threshold, unit] of units) {
        if (secondsAgo >= threshold) {
            const count = Math.floor(secondsAgo / threshold);
            return `${count} ${unit}${count !== 1 ? 's' : ''} ago`;
        }
    }
    const s = Math.max(0, secondsAgo);
    return `${s} second${s !== 1 ? 's' : ''} ago`;
}

export { formatTime, stringToDate, dateToTimeAgo };
