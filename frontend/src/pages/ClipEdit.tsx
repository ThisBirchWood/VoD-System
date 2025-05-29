import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import Playbar from "./../components/Playbar";
import PlaybackSlider from "./../components/PlaybackSlider";
import ClipRangeSlider from "./../components/ClipRangeSlider";
import ClipConfig from "./../components/ClipConfig";
import {editFile, getMetadata, processFile, getProgress} from "../utils/endpoints"
import type { VideoMetadata } from "../utils/types.ts";
import BlueButton from "../components/buttons/BlueButton.tsx";

const ClipEdit = () => {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = `/api/v1/download/input/${id}`
    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [playbackValue, setPlaybackValue] = useState(0);
    const [outputMetadata, setOutputMetadata] = useState<VideoMetadata>({
        // default values
        startPoint: 0,
        endPoint: 5,
        width: 1280,
        height: 720,
        fps: 30,
        fileSize: 10000
    });
    const [progress, setProgress] = useState(0);
    const [downloadable, setDownloadable] = useState(false);

    const sendData = async() => {
        if (!id) return;

        setDownloadable(false);

        await editFile(id, outputMetadata);
        const processed = await processFile(id);

        if (!processed) {
            console.log("Failed to process file");
            return;
        }

        const interval = setInterval(async () => {
           const progress = await getProgress(id);
           setProgress(progress);

           if (progress >= 1) {
               clearInterval(interval);
               setDownloadable(true);
               console.log("Downloadable");
           }
        }, 500);
    }

    const handleDownload = async (filename: string | undefined) => {
        if (!filename) return;

        const response = await fetch(`/api/v1/download/output/${id}`);

        if (!response.ok) {
            console.error('Download failed');
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');

        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    };

    useEffect(() => {
        if (!id) return;

        getMetadata(id)
            .then((data) => setMetadata(data ?? null))
            .catch((err) => console.error("Metadata fetch failed:", err));
    }, [id]);

    return (
        <div className={"grid grid-cols-[70%_30%]"}>
            <video
                   ref={videoRef}
                   className={"w-full rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag. Bzzzz.
            </video>


            <ClipConfig
                setMetadata={setOutputMetadata}
            />

            {metadata &&
                <div>
                    <Playbar
                        video={videoRef.current}
                        videoMetadata={metadata}
                        className={"w-full accent-primary text-text"}
                    />

                    <PlaybackSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        sliderValue={playbackValue}
                        setSliderValue={setPlaybackValue}
                        className={"w-full accent-primary"}
                    />

                    <ClipRangeSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        setSliderValue={setPlaybackValue}
                        setMetadata={setOutputMetadata}
                        className={"w-full mb-10 bg-primary"}
                    />
                </div>}

            <div className={"flex flex-col gap-2 w-4/5 m-auto"}>
                <BlueButton
                    onClick={sendData}>
                    Export
                </BlueButton>

                { downloadable ?
                    (<BlueButton
                        onClick={() => handleDownload(id)}>
                        Download
                    </BlueButton>)
                    :(
                    <progress
                        value={progress}
                        className={"bg-gray-200 rounded-lg h-1"}>
                    </progress> )
                }
            </div>
        </div>
    );
}

export default ClipEdit;