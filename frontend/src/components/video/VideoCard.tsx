import clsx from "clsx";
import { formatTime } from "../../utils/utils.ts";
import { Link } from "react-router-dom";
import { useState } from "react";

type VideoCardProps = {
    title: string,
    duration: number,
    thumbnailPath: string | null,
    videoPath: string,
    className?: string
}

const fallbackThumbnail = "../../../public/default_thumbnail.png";

const VideoCard = ({
                       title,
                       duration,
                       thumbnailPath,
                       videoPath,
                       className
                   }: VideoCardProps) => {

    const initialSrc = thumbnailPath && thumbnailPath.trim() !== "" ? thumbnailPath : fallbackThumbnail;
    const [imgSrc, setImgSrc] = useState(initialSrc);

    return (
        <Link to={videoPath}>
            <div className={clsx("flex flex-col", className)}>
                <img
                    src={imgSrc}
                    alt="Video Thumbnail"
                    onError={() => {
                        if (imgSrc !== fallbackThumbnail) {
                            setImgSrc(fallbackThumbnail);
                        }
                    }}
                />

                <div className={"flex flex-row justify-between items-center p-2"}>
                    <p>{title}</p>
                    <p>{formatTime(duration)}</p>
                </div>
            </div>
        </Link>
    );
}

export default VideoCard;
