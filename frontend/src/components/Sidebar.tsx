import { Link } from "react-router-dom";
import clsx from "clsx";
import MenuButton from "./buttons/MenuButton.tsx";
import { Plus, Film } from 'lucide-react';

type props = {
    className?: string
}

const Sidebar = ({className}: props) => {
    return (
        <div className={clsx("h-screen bg-white shadow-sm border-r flex flex-col gap-2 mr-5", className)}>
            <Link
                className={"w-full"}
                to="/create">
                <MenuButton className={"flex items-center gap-2 w-full"}>
                    <Plus size={20}/> Create Clip
                </MenuButton>
            </Link>
            <MenuButton className={"flex items-center gap-2"}>
                <Film size={20}/> My Clips
            </MenuButton>
        </div>
    );
};

export default Sidebar;