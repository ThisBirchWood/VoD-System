import clsx from "clsx";
import SidebarButton from "./buttons/SidebarButton.tsx";
import { Plus, Film, Home, User as UserIcon } from 'lucide-react';
import type {User} from "../utils/types.ts";

type props = {
    user: User | null;
    className?: string
}

const Sidebar = ({user, className}: props) => {
    return (
        <aside className={clsx("h-screen flex flex-col bg-white border-r border-gray-200", className)}>
            <div className="px-4 pt-5 pb-3">
                <img
                    className="w-36 mx-auto"
                    src="../../public/logo.png"
                    alt="VoD System Logo"
                />
            </div>
            <nav className="flex flex-col flex-1 gap-0.5 px-2 pb-4">
                <SidebarButton url="/" logo={<Home size={18}/>} label="Home" />
                <SidebarButton url="/create" logo={<Plus size={18}/>} label="Create Clip" />
                {user && (
                    <>
                        <SidebarButton url="/my-clips" logo={<Film size={18}/>} label="My Clips" />
                        <SidebarButton url="/profile" logo={<UserIcon size={18}/>} label="Profile" />
                    </>
                )}
            </nav>
        </aside>
    );
};

export default Sidebar;
