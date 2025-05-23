import {ChangeEventHandler, useEffect, useState} from "react";
import { Volume, Play, Pause } from 'lucide-react';

type Props = {
    video: HTMLVideoElement | null;
};

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

export default function Playbar({ video }: Props) {
    const [isPlaying, setIsPlaying] = useState(false);
    const [volume, setVolume] = useState(100);

    const togglePlay = () => {
        if (!video) return;

        if (video.paused) {
            video.play();
            setIsPlaying(true);
        } else {
            video.pause();
            setIsPlaying(false);
        }
    };

    const updateVolume = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!video) return;

        video.volume = parseInt(e.target.value) / 100;
        setVolume(parseInt(e.target.value));
    }

    // Sync state with video element changes (e.g., if someone presses spacebar or clicks on the video)
    useEffect(() => {
        if (!video) return;

        const handlePlay = () => setIsPlaying(true);
        const handlePause = () => setIsPlaying(false);

        video.addEventListener("play", handlePlay);
        video.addEventListener("pause", handlePause);

        return () => {
            video.removeEventListener("play", handlePlay);
            video.removeEventListener("pause", handlePause);
        };
    }, [video]);

    return (
        <div className={"flex justify-between items-center bg-gray-300 p-2 rounded-lg"}>
            <div className={"flex"}>
                <Volume size={24} />
                <input
                    type='range'
                    min={0}
                    max={100}
                    onChange={updateVolume}
                    value={volume}
                    className={"w-20"}
                />
            </div>
            <button onClick={togglePlay}>
                {isPlaying ? <Pause size={24} /> : <Play size={24} />}
            </button>
            <label>
                {formatTime(video?.currentTime ?? 0)} / {formatTime(video?.duration ?? 0)}
            </label>
        </div>
    );
}