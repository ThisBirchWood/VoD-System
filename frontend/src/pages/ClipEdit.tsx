import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import Playbar from "./../components/video/Playbar";
import PlaybackSlider from "./../components/video/PlaybackSlider";
import ClipRangeSlider from "./../components/video/ClipRangeSlider";
import ClipConfig from "./../components/video/ClipConfig";
import ExportWidget from "../components/video/ExportWidget.tsx";
import {editFile, getMetadata, processFile, getProgress} from "../utils/endpoints"
import type { VideoMetadata } from "../utils/types.ts";
import Box from "../components/Box.tsx";
import ClipNames from "../components/video/ClipNames.tsx";

const ClipEdit = () => {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = `/api/v1/download/input/${id}`
    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [playbackValue, setPlaybackValue] = useState(0);
    const [outputMetadata, setOutputMetadata] = useState<VideoMetadata>({
        // default values
        title: "",
        description: "",
        startPoint: 0,
        endPoint: 5,
        width: 1280,
        height: 720,
        fps: 30,
        fileSize: 10000
    });
    const [progress, setProgress] = useState(0);
    const [downloadable, setDownloadable] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const sendData = async() => {
        if (!id) return;

        setDownloadable(false);

        const edited = await editFile(id, outputMetadata, setError);

        if (!edited) {
            return;
        }

        const processed = await processFile(id, setError);

        if (!processed) {
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

    const handleDownload = async () => {
        const response = await fetch(`/api/v1/download/output/${id}`);

        if (!response.ok) {
            console.error('Download failed');
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');

        a.href = url;
        a.download = `${id}.mp4`;
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
        <div className={"grid grid-cols-[7fr_3fr]"}>
            <video
                   ref={videoRef}
                   className={"w-full rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag. Bzzzz.
            </video>


            <Box className={"w-4/5 h-full m-auto"}>
                <ClipNames
                    setMetadata={setOutputMetadata}
                />
                <ClipConfig
                    setMetadata={setOutputMetadata}
                />
            </Box>


            {metadata &&
                <Box className={"mt-4 p-5"}>
                    <Playbar
                        video={videoRef.current}
                        videoMetadata={metadata}
                        className={"w-full accent-primary"}
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
                        className={"w-full mt-2 bg-primary"}
                    />

                    {error && (
                        <div className={"text-red-600 text-center mt-2"}>
                            {error}
                        </div>
                    )}
                </Box>
            }

            <Box className={"flex flex-col gap-2 w-4/5 m-auto mt-4 p-5"}>
                <ExportWidget
                    dataSend={sendData}
                    handleDownload={handleDownload}
                    downloadable={downloadable}
                    progress={progress}
                />
            </Box>
        </div>
    );
}

export default ClipEdit;