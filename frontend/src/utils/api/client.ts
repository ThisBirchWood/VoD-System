const API_URL = import.meta.env.VITE_API_URL;

class AuthError extends Error {
    constructor() { super("Not authenticated"); this.name = "AuthError"; }
}

const isThumbnailAvailable = async (thumbnailUrl: string): Promise<boolean> => {
    const response = await fetch(thumbnailUrl, { credentials: 'include' });
    return response.ok;
};

export { API_URL, AuthError, isThumbnailAvailable };
