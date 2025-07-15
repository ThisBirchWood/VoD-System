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

function stringToDate(dateString: string): Date {
    const validIso = dateString.substring(0, 23);
    const date = new Date(validIso);
    if (isNaN(date.getTime())) {
        throw new Error("Invalid date string");
    }
    return date;
}

function dateToTimeAgo(date: Date): string {
    const now = new Date();
    const secondsAgo = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (secondsAgo < 60) {
        return `${secondsAgo} seconds ago`;
    } else if (secondsAgo < 3600) {
        const minutes = Math.floor(secondsAgo / 60);

        if (minutes === 1) {
            return "1 minute ago";
        }

        return `${minutes} minutes ago`;
    } else if (secondsAgo < 86400) {
        const hours = Math.floor(secondsAgo / 3600);

        if (hours === 1) {
            return "1 hour ago";
        }

        return `${hours} hours ago`;
    } else if (secondsAgo < 2592000) {
        const days = Math.floor(secondsAgo / 86400);

        if (days === 1) {
            return "1 day ago";
        }

        return `${days} days ago`;
    } else if (secondsAgo < 31536000) {
        const months = Math.floor(secondsAgo / 2592000);

        if (months === 1) {
            return "1 month ago";
        }

        return `${months} months ago`;
    } else {
        const years = Math.floor(secondsAgo / 31536000);

        if (years === 1) {
            return "1 year ago";
        }

        return `${years} years ago`;
    }
}

export {
    formatTime,
    stringToDate,
    dateToTimeAgo
}