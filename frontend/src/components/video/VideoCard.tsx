import clsx from "clsx";
import { formatTime } from "../../utils/utils.ts";
import {Link} from "react-router-dom";

type VideoCardProps = {
    title: string,
    length: number,
    thumbnailUrl: string,
    videoUrl: string,
    className?: string
}

const VideoCard = ({
                       title,
                       length,
                       thumbnailUrl,
                       videoUrl,
                       className}: VideoCardProps) => {
    return (
        <Link
        to={videoUrl}
        >
            <div className={clsx("flex flex-col", className)}>
                <img src={thumbnailUrl} alt="Video Thumbnail" />

                <div className={"flex flex-row justify-between items-center p-2"}>
                    <p>{title}</p>
                    <p>{formatTime(length)}</p>
                </div>

            </div>
        </Link>
    );
}

export default VideoCard;