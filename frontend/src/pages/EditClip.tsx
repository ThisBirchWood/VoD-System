import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Check } from "lucide-react";
import { getClipById, patchClip } from "../utils/endpoints.ts";
import Box from "../components/Box.tsx";
import BlueButton from "../components/buttons/BlueButton.tsx";

const inputClass = "border border-gray-300 bg-white rounded-md w-full p-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors";

const EditClip = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        getClipById(id)
            .then((clip) => {
                if (clip) {
                    setTitle(clip.title ?? "");
                    setDescription(clip.description ?? "");
                }
            })
            .catch(() => setError("Failed to load clip."))
            .finally(() => setLoading(false));
    }, [id]);

    const handleSave = async () => {
        if (!id) return;
        setSaving(true);
        setError(null);
        try {
            await patchClip(Number(id), { title, description });
            setSaved(true);
            setTimeout(() => setSaved(false), 2000);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to save.");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="px-8 py-10 max-w-2xl mx-auto">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 mb-6 transition-colors duration-150"
            >
                <ArrowLeft size={16} />
                Back
            </button>

            <h1 className="text-2xl font-semibold text-gray-900 mb-6">Edit Clip</h1>

            {loading ? (
                <div className="text-gray-400 text-sm">Loading...</div>
            ) : (
                <Box className="p-6 flex flex-col gap-4">
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-gray-700">Title</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="Enter title"
                            className={inputClass}
                        />
                    </div>

                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-gray-700">Description</label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Enter description"
                            rows={4}
                            className={inputClass + " resize-none"}
                        />
                    </div>

                    {error && <p className="text-sm text-red-500">{error}</p>}

                    <div className="flex items-center gap-3 pt-1">
                        <BlueButton onClick={handleSave} disabled={saving} className="w-28">
                            {saving ? "Saving…" : saved
                                ? <span className="flex items-center gap-1.5 justify-center"><Check size={14} />Saved</span>
                                : "Save"}
                        </BlueButton>
                        <button
                            onClick={() => navigate(-1)}
                            className="text-sm text-gray-500 hover:text-gray-900 transition-colors duration-150"
                        >
                            Cancel
                        </button>
                    </div>
                </Box>
            )}
        </div>
    );
};

export default EditClip;
