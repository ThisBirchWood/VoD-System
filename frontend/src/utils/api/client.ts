import {config} from "../../config.ts";

const API_URL = config.apiUrl;

class AuthError extends Error {
    constructor() { super("Not authenticated"); this.name = "AuthError"; }
}

let onUnauthorized: (() => void) | null = null;

export function registerUnauthorizedHandler(handler: () => void) {
    onUnauthorized = handler;
}

/**
 * Performs a request against the API, sending session cookies and funnelling
 * expired/missing sessions through the registered unauthorized handler so the
 * UI can drop back to a logged out state. Returns the raw response so callers
 * that need something other than JSON (blobs, streams) can read it themselves.
 */
async function apiFetchRaw(url: string, options: RequestInit = {}): Promise<Response> {
    const response = await fetch(url, {
        ...options,
        credentials: 'include'
    });

    if (response.status == 401 || response.status == 403) {
        onUnauthorized?.();
        throw new AuthError();
    }

    if (!response.ok) {
        const text = await response.text();
        let message = text;
        try { message = JSON.parse(text).message ?? text; } catch { /* not JSON, use raw text */ }
        throw new Error(message || `Request failed: ${response.status}`);
    }

    return response;
}

async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
    const response = await apiFetchRaw(url, options);
    const text = await response.text();
    return text ? JSON.parse(text) : (undefined as T);
}

export { API_URL, AuthError, apiFetch, apiFetchRaw };
