import clsx from "clsx";
import { formatTime, stringToDate, dateToTimeAgo } from "../../utils/utils.ts";
import { Link } from "react-router-dom";
import {useEffect, useState} from "react";

type VideoCardProps = {
    id: number,
    title: string,
    duration: number,
    thumbnailPath: string | null,
    videoPath: string,
    createdAt: string,
    className?: string
}

const fallbackThumbnail = "../../../public/default_thumbnail.png";

const VideoCard = ({
                       id,
                       title,
                       duration,
                       createdAt,
                       className
                   }: VideoCardProps) => {

    const [timeAgo, setTimeAgo] = useState(dateToTimeAgo(stringToDate(createdAt)));

    setTimeout(() => {
        setTimeAgo(dateToTimeAgo(stringToDate(createdAt)))
    }, 1000);

    return (
        <Link to={"/video/" + id}>
            <div className={clsx("flex flex-col", className)}>
                <div className={"relative inline-block"}>
                    <img
                        src={`/api/v1/download/thumbnail/${id}`}
                        alt="Video Thumbnail"
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
                </div>


                <div className={"flex flex-col justify-between p-2"}>
                    <p>{title == "" ? "(No Title)" : title}</p>
                    <p
                        className={"text-gray-600 text-sm"}
                    >
                        {timeAgo}
                    </p>
                </div>
            </div>
        </Link>
    );
}

export default VideoCard;
