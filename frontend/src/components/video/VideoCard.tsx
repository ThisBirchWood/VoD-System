import clsx from "clsx";
import { formatTime, stringToDate, dateToTimeAgo } from "../../utils/utils.ts";
import { Link } from "react-router-dom";
import {useEffect, useState} from "react";

type VideoCardProps = {
    id: number,
    title: string,
    duration: number,
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
    const [thumbnailAvailable, setThumbnailAvailable] = useState(true);

    setTimeout(() => {
        setTimeAgo(dateToTimeAgo(stringToDate(createdAt)))
    }, 1000);

    useEffect(() => {
        // Check if thumbnail is available
        const checkThumbnail = async () => {
            try {
                const response = await fetch(`/api/v1/download/thumbnail/${id}`);
                if (!response.ok) {
                    throw new Error("Thumbnail not found");
                }
            } catch (error) {
                setThumbnailAvailable(false);
            }
        };

        checkThumbnail();
    }, []);

    return (
        <Link to={"/video/" + id}>
            <div className={clsx("flex flex-col", className)}>
                <div className={"relative inline-block"}>
                    <img
                        src={thumbnailAvailable ? `/api/v1/download/thumbnail/${id}` : fallbackThumbnail}
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
