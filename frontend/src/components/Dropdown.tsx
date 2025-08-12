import React, {useEffect, useRef} from "react";
import clsx from "clsx";

type DropdownItemProps = {
    item: string;
    onClick: (item: string) => void;
    className?: string;
}

const DropdownItem = ({ item, onClick, className }: DropdownItemProps) => {
    return (
        <li className={clsx(className, "cursor-pointer hover:bg-gray-100 px-4 py-2 align-middle")}
            onClick={() => onClick(item)}
        >
            {item}
        </li>
    );
}

type DropdownProps = {
    label: string;
    children: React.ReactNode;
    className?: string;
}

const Dropdown = ({ label, children, className }: DropdownProps) => {
    const [isOpen, setIsOpen] = React.useState(false);
    const ref = useRef<HTMLDivElement>(null);

    const toggleDropdown = () => {
        setIsOpen(!isOpen);
    };

    useEffect(() => {
        function handleClickOutside(event: { target: any }) {
            if (ref.current && !ref.current.contains(event.target)) {
                setIsOpen(false);
            }
        }

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    return (
        <div className={clsx(className, "relative inline-block text-left")}
            ref={ref}>
            <button
                onClick={toggleDropdown}
                className={"inline-flex justify-between w-full rounded-xl px-4 py-2 bg-white text-sm font-medium text-gray-600 hover:bg-gray-50"}
            >
                {label}
            </button>
            {isOpen && (
                <ul className={"absolute w-30 origin-top-right rounded-md bg-white shadow-lg font-medium"}>
                    {children}
                </ul>
            )}
        </div>
    );
};



export { Dropdown, DropdownItem };