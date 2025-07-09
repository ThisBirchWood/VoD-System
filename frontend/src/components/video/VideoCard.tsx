import clsx from "clsx";
import { formatTime, stringToDate, dateToTimeAgo } from "../../utils/utils.ts";
import { Link } from "react-router-dom";
import { useState } from "react";

type VideoCardProps = {
    title: string,
    duration: number,
    thumbnailPath: string | null,
    videoPath: string,
    createdAt: string,
    className?: string
}

const fallbackThumbnail = "../../../public/default_thumbnail.png";

const VideoCard = ({
                       title,
                       duration,
                       thumbnailPath,
                       videoPath,
                       createdAt,
                       className
                   }: VideoCardProps) => {

    const initialSrc = thumbnailPath && thumbnailPath.trim() !== "" ? thumbnailPath : fallbackThumbnail;
    const [imgSrc, setImgSrc] = useState(initialSrc);

    return (
        <Link to={videoPath}>
            <div className={clsx("flex flex-col", className)}>
                <div className={"relative inline-block"}>
                    <img
                        src={imgSrc}
                        alt="Video Thumbnail"
                        onError={() => {
                            if (imgSrc !== fallbackThumbnail) {
                                setImgSrc(fallbackThumbnail);
                            }
                        }}
                    />

                    <p className="
                            absolute
                            top-2
                            left-2
                            bg-black bg-opacity-60
                            text-white
                            px-2
                            py-1
                            rounded
                            pointer-events-none
                            text-sm
                            z-1
                          ">
                        {formatTime(duration)}
                    </p>

                    <p>
                        {dateToTimeAgo(stringToDate(createdAt))}
                    </p>
                </div>


                <div className={"flex flex-row justify-between items-center p-2"}>
                    <p>{title == "" ? "(No Title)" : title}</p>
                </div>
            </div>
        </Link>
    );
}

export default VideoCard;
