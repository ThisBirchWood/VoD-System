import clsx from "clsx";
import { formatTime } from "../../utils/utils.ts";
import {Link} from "react-router-dom";

type VideoCardProps = {
    title: string,
    duration: number,
    thumbnailPath: string,
    videoPath: string,
    className?: string
}

const VideoCard = ({
                       title,
                       duration,
                       thumbnailPath,
                       videoPath,
                       className}: VideoCardProps) => {
    return (
        <Link
        to={videoPath}
        >
            <div className={clsx("flex flex-col", className)}>
                <img src={thumbnailPath} alt="Video Thumbnail" />

                <div className={"flex flex-row justify-between items-center p-2"}>
                    <p>{title}</p>
                    <p>{formatTime(duration)}</p>
                </div>

            </div>
        </Link>
    );
}

export default VideoCard;