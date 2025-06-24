import clsx from "clsx";
import SidebarButton from "./buttons/SidebarButton.tsx";
import { Plus, Film, Home } from 'lucide-react';
import Box from "./Box.tsx";
import type {User} from "../utils/types.ts";

type props = {
    user: User | null;
    className?: string
}

const Sidebar = ({user, className}: props) => {
    return (
        <Box className={clsx("h-screen flex flex-col mr-5", className)}>
            <img
                className={"w-45 mx-auto"}
                src={"../../public/logo.png"}
                alt={"VoD System Logo"}
            />
            <SidebarButton
                url={"/"}
                logo={<Home size={20}/>}
                label={"Home"}
            />

            <SidebarButton
                url={"/create"}
                logo={<Plus size={20}/>}
                label={"Create Clip"}
            />

            { user && (
                <SidebarButton
                    url={"/my-clips"}
                    logo={<Film size={20}/>}
                    label={"My Clips"}
                />
                )}
        </Box>
    );
};

export default Sidebar;