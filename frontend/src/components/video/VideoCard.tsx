import clsx from "clsx";
import { formatTime, stringToDate, dateToTimeAgo } from "../../utils/utils.ts";
import { Link } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import { isThumbnailAvailable } from "../../utils/endpoints.ts";
import { MoreVertical, Pencil, Trash2 } from "lucide-react";

type VideoCardProps = {
    id: number,
    title: string,
    duration: number,
    createdAt: string,
    onEdit?: () => void,
    onDelete?: () => void,
    className?: string,
}

const fallbackThumbnail = "../../../public/default_thumbnail.png";
const API_URL = import.meta.env.VITE_API_URL;

const VideoCard = ({ id, title, duration, createdAt, onEdit, onDelete, className }: VideoCardProps) => {
    const [timeAgo, setTimeAgo] = useState(dateToTimeAgo(stringToDate(createdAt)));
    const [thumbnailAvailable, setThumbnailAvailable] = useState(true);
    const [menuOpen, setMenuOpen] = useState(false);
    const [confirmDelete, setConfirmDelete] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    setTimeout(() => {
        setTimeAgo(dateToTimeAgo(stringToDate(createdAt)));
    }, 1000);

    useEffect(() => {
        isThumbnailAvailable(id)
            .then(setThumbnailAvailable)
            .catch(() => setThumbnailAvailable(false));
    }, []);

    useEffect(() => {
        if (!menuOpen) return;
        const handleClickOutside = (e: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
                setMenuOpen(false);
                setConfirmDelete(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [menuOpen]);

    const stopAndRun = (e: React.MouseEvent, fn: () => void) => {
        e.preventDefault();
        e.stopPropagation();
        fn();
    };

    return (
        <Link to={"/video/" + id}>
            <div className={clsx("flex flex-col group cursor-pointer", className)}>
                <div className="relative overflow-hidden rounded-lg">
                    <img
                        src={thumbnailAvailable ? API_URL + `/api/v1/download/thumbnail/${id}` : fallbackThumbnail}
                        alt="Video Thumbnail"
                        className="w-full aspect-video object-cover group-hover:scale-105 transition-transform duration-200"
                    />

                    <span className="absolute top-1.5 left-1.5 bg-black/70 text-white px-1.5 py-0.5 rounded text-xs font-medium pointer-events-none">
                        {formatTime(duration)}
                    </span>

                    {(onEdit || onDelete) && (
                        <div
                            ref={menuRef}
                            className="absolute top-1.5 right-1.5"
                            onClick={(e) => e.preventDefault()}
                        >
                            <button
                                onClick={(e) => stopAndRun(e, () => { setMenuOpen(v => !v); setConfirmDelete(false); })}
                                className="bg-black/70 hover:bg-black/90 text-white rounded p-0.5 transition-colors"
                            >
                                <MoreVertical size={14} />
                            </button>

                            {menuOpen && (
                                <div className="absolute right-0 top-7 w-32 bg-white rounded-lg border border-gray-200 shadow-md z-50 py-1 overflow-hidden">
                                    {!confirmDelete && (<>
                                        {onEdit && (
                                            <button
                                                onClick={(e) => stopAndRun(e, onEdit)}
                                                className="flex items-center gap-2 w-full px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                                            >
                                                <Pencil size={13} /> Edit
                                            </button>
                                        )}
                                        {onDelete && (
                                            <button
                                                onClick={(e) => stopAndRun(e, () => setConfirmDelete(true))}
                                                className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-500 hover:bg-red-50"
                                            >
                                                <Trash2 size={13} /> Delete
                                            </button>
                                        )}
                                    </>)}
                                    {confirmDelete && (
                                        <div className="px-3 py-2">
                                            <p className="text-xs text-gray-600 mb-2">Delete this clip?</p>
                                            <div className="flex gap-1.5">
                                                <button
                                                    onClick={(e) => stopAndRun(e, onDelete!)}
                                                    className="flex-1 text-xs bg-red-500 text-white rounded px-2 py-1 hover:bg-red-600 transition-colors"
                                                >
                                                    Delete
                                                </button>
                                                <button
                                                    onClick={(e) => stopAndRun(e, () => setConfirmDelete(false))}
                                                    className="flex-1 text-xs bg-gray-100 text-gray-600 rounded px-2 py-1 hover:bg-gray-200 transition-colors"
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="flex flex-col p-2">
                    <p className="font-medium text-gray-900 text-sm leading-snug line-clamp-2">
                        {title === "" ? "(No Title)" : title}
                    </p>
                    <p className="text-gray-500 text-xs mt-0.5">{timeAgo}</p>
                </div>
            </div>
        </Link>
    );
}

export default VideoCard;
