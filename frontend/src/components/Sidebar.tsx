import clsx from "clsx";
import SidebarButton from "./buttons/SidebarButton.tsx";
import { Plus, Film, Home, User as UserIcon } from 'lucide-react';
import type {User} from "../utils/types.ts";

type props = {
    user: User | null;
    className?: string
}

const Divider = () => <hr className="my-1.5 mx-2 border-gray-100" />;

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
            <nav className="flex flex-col flex-1 px-2 pb-4">
                <div className="flex flex-col gap-0.5">
                    <SidebarButton url="/" logo={<Home size={18}/>} label="Home" />
                    <SidebarButton url="/create" logo={<Plus size={18}/>} label="Create Clip" />
                </div>

                {user && (
                    <>
                        <Divider />
                        <div className="flex flex-col gap-0.5">
                            <SidebarButton url="/my-clips" logo={<Film size={18}/>} label="Clips" />
                            {/*<SidebarButton url="/vods" logo={<Video size={18}/>} label="VoDs" />*/}
                        </div>
                    </>
                )}

                <div className="flex-1" />

                {user && (
                    <>
                        <Divider />
                        <SidebarButton url="/profile" logo={<UserIcon size={18}/>} label="Profile" />
                    </>
                )}
            </nav>
        </aside>
    );
};

export default Sidebar;
