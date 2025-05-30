import { Link } from "react-router-dom";
import clsx from "clsx";
import MenuButton from "./buttons/MenuButton.tsx";
import { Plus, Film, Home } from 'lucide-react';
import Box from "./Box.tsx";

type props = {
    className?: string
}

const Sidebar = ({className}: props) => {
    return (
        <Box className={clsx("h-screen  flex flex-col gap-2 mr-5", className)}>
            <img
                className={"w-45 mx-auto grayscale-100"}
                src={"../../public/logo.png"}
                alt={"VoD System Logo"}
            />
            <Link
                className={"w-full"}
                to={"/"}
            >
                <MenuButton className={"flex items-center gap-2 w-full"}>
                    <Home size={20}/> Home
                </MenuButton>
            </Link>

            <Link
                className={"w-full"}
                to="/create"
            >
                <MenuButton className={"flex items-center gap-2 w-full"}>
                    <Plus size={20}/> Create Clip
                </MenuButton>
            </Link>
            <MenuButton className={"flex items-center gap-2"}>
                <Film size={20}/> My Clips
            </MenuButton>
        </Box>
    );
};

export default Sidebar;