import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import { VideoMetadata } from "Frontend/components/Playbar";
import Playbar from "./../../components/Playbar";
import PlaybackSlider from "./../../components/PlaybackSlider";
import ClipRangeSlider from "./../../components/ClipRangeSlider";
import ClipConfig from "./../../components/ClipConfig";

function exportFile(uuid: string,
                    startPoint: number,
                    endPoint: number,
                    width: number,
                    height: number,
                    fps: number,
                    fileSize: number) {
    var body: string = `startPoint=${startPoint}&endPoint=${endPoint}&width=${width}&height=${height}&fps=${fps}&fileSize=${fileSize*1000}`;

    fetch(`api/v1/edit/${uuid}`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: body,
    })
        .then(res => {
            console.log("RESPONSE: " + res);
            return res
        })
        .then(data => console.log('Response:', data))
        .catch(err => console.error('Error:', err));

    return null;
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

    useEffect(() => {
        fetch(`api/v1/metadata/original/${id}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch metadata");
                return res.json();
            })
            .then(setMetadata)
            .catch((err) => console.log(err.message));
    }, [id]);

    const sendData = () => {
        if (!id) return
        exportFile(id,clipRangeValue[0], clipRangeValue[1], width, height, fps, fileSize);
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

            <button
                className={"bg-primary text-text p-2 rounded-lg hover:bg-primary-pressed h-10 w-3/4 m-auto"}
                onClick={sendData}
            >Export</button>

        </div>
    );
}