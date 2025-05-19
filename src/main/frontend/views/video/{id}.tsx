import { useParams } from 'react-router-dom';

export default function video() {
    const { id } = useParams();
    const videoUrl = "api/v1/download/input/" + id;

    return (
        <div className={"flex justify-around"}>
            <video controls
                   width="600"
                    className={"w-full max-w-3xl rounded-lg shadow-lg border border-gray-300 bg-black"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag.
            </video>
        </div>
    );
}