import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import Playbar from "./../../components/Playbar";
import PlaybackSlider from "./../../components/PlaybackSlider";
import ClipRangeSlider from "./../../components/ClipRangeSlider";
import ClipConfig from "./../../components/ClipConfig";
import * as editService from "../../generated/EditService";
import * as metadataService from "../../generated/MetadataService"
import VideoMetadata from "Frontend/generated/com/ddf/vodsystem/entities/VideoMetadata";

function exportFile(uuid: string,
                    startPoint: number,
                    endPoint: number,
                    width: number,
                    height: number,
                    fps: number,
                    fileSize: number,
                    setProgress: Function) {

    const metadata: VideoMetadata = {
        startPoint: startPoint,
        endPoint: endPoint,
        width: width,
        height: height,
        fps: fps,
        fileSize: fileSize*1000
    }

    editService.edit(uuid, metadata)
        .then(r => {
            editService.process(uuid);
        });

    // get progress updates
    const interval = setInterval(async () => {
        try {
            const result = await editService.getProgress(uuid);
            setProgress(result);

            if (result >= 1) {
                clearInterval(interval);
                console.log('Progress complete');
            }
        } catch (err) {
            console.error('Failed to fetch progress', err);
            clearInterval(interval);
        }
    }, 500); // 0.5 seconds
}

export default function VideoId() {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = `api/v1/download/input/${id}`

    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [playbackValue, setPlaybackValue] = useState(0);
    const [clipRangeValue, setClipRangeValue] = useState([0, 1]);
    const [width, setWidth] = useState(1280);
    const [height, setHeight] = useState(720);
    const [fps, setFps] = useState(30);
    const [fileSize, setFileSize] = useState(10);

    const [progress, setProgress] = useState(0);

    useEffect(() => {
        if (!id) return;

        metadataService.getInputFileMetadata(id)
            .then((data) => setMetadata(data ?? null)) // 👈 Normalize undefined to null
            .catch((err) => console.error("Metadata fetch failed:", err));
    }, [id]);

    const sendData = () => {
        if (!id) return
        exportFile(id,clipRangeValue[0], clipRangeValue[1], width, height, fps, fileSize, setProgress);
    }

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
                setWidth={setWidth}
                setHeight={setHeight}
                setFileSize={setFileSize}
                setFps={setFps}
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
                        setClipRangeValue={setClipRangeValue}
                        className={"w-full mb-10 bg-primary"}
                    />
                </div>}

            <div className={"flex flex-col gap-2 w-4/5 m-auto"}>
                <button
                    className={"bg-primary text-text p-2 rounded-lg hover:bg-primary-pressed h-10"}
                    onClick={sendData}>
                    Export
                </button>

                <progress
                    value={progress}
                    className={"bg-gray-200 rounded-lg h-1"}>
                </progress>
            </div>


        </div>
    );
}