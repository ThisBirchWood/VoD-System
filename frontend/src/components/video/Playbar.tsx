import { useEffect, useState} from "react";
import { Volume1, Volume2, VolumeX, Play, Pause } from 'lucide-react';
import clsx from 'clsx';
import type { VideoMetadata } from "../../utils/types.ts";
import { formatTime } from "../../utils/utils.ts";


type Props = {
    video: HTMLVideoElement | null;
    videoMetadata: VideoMetadata;
    className?: string;
};

export default function Playbar({ video, videoMetadata, className }: Props) {
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

    let Icon;
    // update icon
    if (volume == 0) {
        Icon = VolumeX;
    } else if (volume < 50) {
        Icon = Volume1;
    } else {
        Icon = Volume2;
    }

    const updateVolume = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!video) return;

        let volume = parseInt(e.target.value)

        video.volume = volume / 100;
        setVolume(volume);
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
        <div className={clsx("flex justify-between items-center mb-2", className)}>
            <div className={"flex gap-1"}>
                <Icon size={24} />
                <input
                    type='range'
                    min={0}
                    max={100}
                    onChange={updateVolume}
                    value={volume}
                    className={"w-20"}
                />
            </div>
            <button
                onClick={togglePlay}
                className={"hover:bg-gray-300 rounded-full p-1"}
            >
                {isPlaying ? <Pause size={24} /> : <Play size={24} />}
            </button>
            {videoMetadata.endPoint &&
                <label>
                    {formatTime(video?.currentTime ?? 0)} / {formatTime(videoMetadata.endPoint)}
                </label>
            }
        </div>
    );
}