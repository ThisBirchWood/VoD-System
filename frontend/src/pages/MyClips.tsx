import VideoCard from "../components/video/VideoCard";
import {useEffect, useState} from "react";
import { getClips } from "../utils/endpoints";
import type { Clip } from "../utils/types";

const MyClips = () => {
    const [clips, setClips] = useState<Clip[]>([]);
    const [error, setError] = useState<null | string>(null);

    useEffect(() => {
        getClips()
            .then((data) => setClips(data))
            .catch((err) => setError(err));
    }, []);

    return (
        <div className={"flex flex-row"}>
            {clips.map((clip) => (
                <VideoCard
                    id={clip.id}
                    title={clip.title}
                    duration={clip.duration}
                    createdAt={clip.createdAt}
                    className={"w-40 m-5"}
                />
            ))}

            {error}
        </div>
    );
}

export default MyClips;
