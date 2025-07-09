import VideoCard from "../components/video/VideoCard";

const MyClips = () => {
    return (
        <div>
            <VideoCard
                title={"My First Clip"}
                length={120}
                thumbnailUrl={"https://upload.wikimedia.org/wikipedia/commons/1/19/Billy_Joel_Shankbone_NYC_2009.jpg"}
                videoUrl={"https://www.youtube.com/watch?v=dQw4w9WgXcQ"}
                className={"w-40"}
            />


        </div>
    );
}

export default MyClips;
