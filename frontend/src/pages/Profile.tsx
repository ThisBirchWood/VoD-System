import { useState, useEffect } from "react";
import { Eye, EyeOff, Copy, Check } from "lucide-react";
import Box from "../components/Box.tsx";
import { getUser } from "../utils/endpoints.ts";
import type { User } from "../utils/types.ts";
import { formatLocalDate } from "../utils/utils.ts";

const Profile = () => {
    const [user, setUser] = useState<User | null>(null);
    const [keyVisible, setKeyVisible] = useState(false);
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        getUser().then(setUser).catch(console.error);
    }, []);

    const handleCopy = () => {
        if (!user?.streamKey) return;
        navigator.clipboard.writeText(user.streamKey);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    if (!user) {
        return (
            <div className="flex justify-center items-center h-full text-gray-400 text-base">
                Please log in to view your profile.
            </div>
        );
    }

    return (
        <div className="px-8 py-10 max-w-2xl mx-auto">
            <h1 className="text-2xl font-semibold text-gray-900 mb-6">Profile</h1>

            <Box className="p-6 mb-4 flex items-center gap-5">
                <img
                    src={user.profilePictureUrl}
                    referrerPolicy="no-referrer"
                    alt="Profile picture"
                    className="w-20 h-20 rounded-full shadow-sm"
                />
                <div>
                    <p className="text-lg font-semibold text-gray-900">{user.name}</p>
                    <p className="text-sm text-gray-500">@{user.username}</p>
                    <p className="text-sm text-gray-500">{user.email}</p>
                    <p className="text-xs text-gray-400 mt-1">
                        Member since {formatLocalDate(user.createdAt)}
                    </p>
                </div>
            </Box>

            <Box className="p-6">
                <h2 className="text-sm font-semibold text-gray-800 mb-1">Stream Key</h2>
                <p className="text-sm text-gray-500 mb-4">
                    Use this key in OBS or any RTMP-compatible software to start streaming.
                    Keep it private — anyone with this key can stream to your account.
                </p>

                <div className="flex items-center gap-2">
                    <div className="flex-1 bg-gray-50 border border-gray-200 rounded-md px-4 py-2 font-mono text-sm text-gray-700 overflow-hidden">
                        {keyVisible ? user.streamKey : "•".repeat(user.streamKey.length)}
                    </div>

                    <button
                        onClick={() => setKeyVisible(v => !v)}
                        title={keyVisible ? "Hide key" : "Show key"}
                        className="p-2 rounded-md bg-gray-100 hover:bg-gray-200 transition-colors duration-150 text-gray-600"
                    >
                        {keyVisible ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>

                    <button
                        onClick={handleCopy}
                        title="Copy key"
                        className="p-2 rounded-md bg-gray-100 hover:bg-gray-200 transition-colors duration-150 text-gray-600"
                    >
                        {copied ? <Check size={16} className="text-green-600" /> : <Copy size={16} />}
                    </button>
                </div>
            </Box>
        </div>
    );
};

export default Profile;
