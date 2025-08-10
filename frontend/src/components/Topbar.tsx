import { Menu, X } from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";
import type {User} from "../utils/types.ts";
import { Dropdown, DropdownItem } from "./Dropdown.tsx";

type props = {
    sidebarToggled: boolean;
    setSidebarToggled: Function;
    user: User | null;
    className?: string;
}

const Topbar = ({sidebarToggled, setSidebarToggled, user, className}: props) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    const loginUrl = `${apiUrl}/oauth2/authorization/google`;
    const logoutUrl = `${apiUrl}/api/v1/auth/logout`;

    return (
        <div className={clsx(className, "flex justify-between")}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>

            { user ? (
                <div>
                    <img
                        className={"w-8 h-8 rounded-full inline-block"}
                        src={user.profilePicture}
                        referrerPolicy="no-referrer"
                    />

                    <Dropdown label={user.name}>
                        <DropdownItem item="Logout"
                                      onClick={() => globalThis.location.href = logoutUrl}
                                      className={"text-red-500 font-medium"} />
                    </Dropdown>
                </div>
            ) :
            (
                <MenuButton className={"w-20 text-gray-600"}
                    onClick={() => globalThis.location.href = loginUrl}>
                    Login
                </MenuButton>
            )}

        </div>
    )
}

export default Topbar;